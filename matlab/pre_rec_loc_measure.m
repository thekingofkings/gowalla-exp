c = 2;
dml4 = importdata('../weightedFrequency-1000u.txt');
[~, ind] = sort(dml4(:,4));
dml4 = dml4(ind, :);

dml5 = importdata('../delete_this-u1000c1.50000.txt');
[~, ind] = sort(dml5(:,6));
dml5 = dml5(ind, :);

for condition = 1 ;
    

%     dml4 = dml4(dml4(:,4) > condition,:);
%     locwf4 = dml4(:,3);
%     locf4 = dml4(:,4);
%     colcEnt = dml4(:,5);
%     dl4 = dml4(:,6);
   


    dml5 = dml5(dml5(:,6) == condition,:);

    prod_colcEnt_cm = dml5(:,3);
    locwf5 = dml5(:,4);
    locm5 = dml5(:,5);
    locf5 = dml5(:,6);
    dl5 = dml5(:,7);


    % Use the prec-recal function from Internet.
%     figure();
%     prec_rec( locwf5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'r--' );
%     hold on;
%     prec_rec( prod_colcEnt_cm, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'g-' );
%     prec_rec( locm5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'b:');
%     prec_rec( locf5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'c--');
    % My own precision-recall plot function    
    figure();
    hold on;
    precisionRecallPlot( locwf5, dl5, 'r--' );
    precisionRecallPlot( prod_colcEnt_cm, dl5, 'k:' );
    precisionRecallPlot( locm5, dl5, 'g-' );
    precisionRecallPlot( locf5, dl5, 'c--' );


    title(num2str(condition));
    box on;
    grid on;
%     axis([0,1,0.5,1]);
    hline = findobj(gcf, 'type', 'line');
    set(hline, 'linewidth', 3);
    xlabel('Recall', 'fontsize', 20);
    ylabel('Precision', 'fontsize', 20);
    set(gca, 'linewidth', 2, 'fontsize', 18);
    legend({'weighted frequency', 'product', '1 - exp measure', 'freq'}, 'location', 'best');
    %    'Location ID measure', 'Location ID frequency'}, 'fontsize', 16);
    set(gcf, 'PaperUnits', 'inches');
    % print(['prl-50m', num2str(c), 'c1000u.eps'], '-dpsc');
    % system(['epstopdf prl-50m', num2str(c), 'c1000u.eps']);
%     saveas(gcf, ['freq-wfbu5000fgt',num2str(condition),'.png']);
%     saveas(gcf, ['freq-wfbu5000fgt',num2str(condition),'.fig']);
end