c = 2;
dml4 = importdata('../weightedFrequency-1000u.txt');
[~, ind] = sort(dml4(:,4));
dml4 = dml4(ind, :);



for condition = 0:5;
    dml5 = importdata('../distance-d30-u5000c1.50000.txt');
    dml6 = importdata('../distance-d30-u5000c1.50000-101s.txt');
    dml6 = dml6(dml6(:,6) > condition, :);
    [~, ind] = sort(dml5(:,6));
    dml5 = dml5(ind, :);

%     dml4 = dml4(dml4(:,4) > condition,:);
%     locwf4 = dml4(:,3);
%     locf4 = dml4(:,4);
%     colcEnt = dml4(:,5);
%     dl4 = dml4(:,6);
   


    dml5 = dml5(dml5(:,6) > condition,:);
    sum(dml5(:,7)==1)
    sum(dml5(:,7)==0)
    size(dml5)

    prod_colcEnt_cm = dml5(:,3);
    locwf5 = dml5(:,4);
    locm5 = dml5(:,5);
    locf5 = dml5(:,6);
    dl5 = dml5(:,7);
    pgt_score = dml6(:,3);
    pgt_lable = dml6(:,7);


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
    precisionRecallPlot( locf5, dl5, 'linestyle', '-', 'color', [0, 0, 0.8] );
    precisionRecallPlot( locm5, dl5, 'r--' );
    precisionRecallPlot( locwf5, dl5, 'linestyle', '--', 'color', [0, 0.75, 0] );
    precisionRecallPlot( prod_colcEnt_cm, dl5, 'linestyle', '-', 'color', [0.3, 0.6, 0.9] );
    precisionRecallPlot( pgt_score, pgt_lable, 'linestyle', '-.', 'color', [0.5, 0.4, 0.9] );


%     title(num2str(condition));
    box on;
    grid on;
%     axis([0,1,0.5,1]);
    hline = findobj(gcf, 'type', 'line');
    set(hline, 'linewidth', 3);
    xlabel('Recall', 'fontsize', 20);
    ylabel('Precision', 'fontsize', 20);
    set(gca, 'linewidth', 2, 'fontsize', 18);
    legend({'Frequency', 'Personal', 'Global', 'Per+Glo', 'Per+Glo+Tem'}, 'location', 'southwest');
    %    'Location ID measure', 'Location ID frequency'}, 'fontsize', 16);
    set(gcf, 'PaperUnits', 'inches');
    print(['pr-', num2str(condition), 'c1000u.eps'], '-dpsc');
    system(['epstopdf pr-', num2str(condition), 'c1000u.eps']);
%     saveas(gcf, ['dist-wsum-d30-u5000fgt',num2str(condition),'.png']);
%     saveas(gcf, ['freq-wfbu5000fgt',num2str(condition),'.fig']);
end