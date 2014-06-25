b = importdata('../data/Pair-glob-measure.txt');

freq = [2, 5, 10];
bin = [1, 10];

countF = zeros(length(freq), length(bin)+1);
countNF = zeros(length(freq), length(bin)+1);

stackedGroup = zeros(length(freq), 2, length(bin)+1);
    
for freq_ind = 1:length(freq)
    fb = b(b(:,2)==1,:);
    nfb = b(b(:,2)==0,:);
    fb = fb(fb(:,3)==freq(freq_ind),1);
    cnt_fb = length(fb);
    nfb = nfb(nfb(:,3)==freq(freq_ind),1);
    cnt_nfb = length(nfb);


    % figure;
    % hold on;
    % box on;
    % l1 = cdfplot(fb);
    % set(l1, 'color', 'blue', 'linewidth', 3);
    % l2 = cdfplot(nfb);
    % set(l2, 'color', 'red', 'linewidth', 3, 'linestyle', '--');
    % set(gca, 'linewidth', 2, 'fontsize', 18);
    % xlabel('Time between adjacent meeting (day)', 'fontsize', 20);
    % ylabel('Count', 'fontsize', 20);
    % title('');
    % legend({'Friend Pair', 'Non-friend Pair'}, 'location', 'northeast', 'fontsize', 16);


    totalF = size(fb, 1);
    totalNF = length(nfb);

    for ind = 1:length(bin)
        fb = fb(fb > bin(ind));
        countF(freq_ind, ind) = totalF - size(fb, 1);
        totalF = size(fb, 1);

        nfb = nfb(nfb > bin(ind));
        countNF(freq_ind, ind) = totalNF - length(nfb);
        totalNF = length(nfb);
    end

    countF(freq_ind, ind+1) = size(fb, 1);
    countNF(freq_ind, ind+1) = length(nfb);


    countF(freq_ind,:) = countF(freq_ind,:) / cnt_fb;
    countNF(freq_ind,:) = countNF(freq_ind,:) / cnt_nfb;

    res = [countF(freq_ind,:); countNF(freq_ind,:)];

    stackedGroup(freq_ind,:,:) = res;
end


plotBarStackGroups(stackedGroup, {'Freq=2', 'Freq=5', 'Freq=10'});
box on;
% xlabel('Time between adjacent meeting (day)', 'fontsize', 20);
% ylabel('Count', 'fontsize', 20);
axis([0,4, 0, 1.4])
text(2.45, 1.05, 'Friend  Nonfriend', 'fontsize', 20);
set(gca, 'linewidth', 3, 'fontsize', 20, 'yticklabel', {0:0.2:1, ''});
legend({'Meeting time gap <1 day', 'Meeting time gap 1-10 days', ...
    'Meeting time gap >10 days'}, 'location', 'northwest', 'fontsize', 20);
print('gw-dtHist.eps', '-dpsc');
system('epstopdf gw-dtHist.eps');